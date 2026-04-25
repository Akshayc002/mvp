import { AxiosError } from 'axios';

interface ProblemDetail {
  detail?: string;
  message?: string;
  title?: string;
}

export const getApiErrorMessage = (error: unknown, fallback = 'Something went wrong') => {
  if (error instanceof AxiosError) {
    const data = error.response?.data as ProblemDetail | undefined;
    return data?.detail || data?.message || data?.title || error.message || fallback;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallback;
};
